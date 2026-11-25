define('DS/Recipes/personPicker/personPicker', [
  'UWA/Core',
  'DS/lodash-4.18.1/lodash',
  'DS/WUXAutoComplete/AutoComplete',
  'DS/TreeModel/TreeNodeModel',
  'DS/Controls/Button',
  'DS/SNInfraUX/SearchCom',
  'DS/i3DXCompassPlatformServices/i3DXCompassPlatformServices',
  'DS/WAFData/WAFData',
  'UWA/Element'
], (UWA, _, AutoComplete, TreeNodeModel, Button, SearchCom, CompassServices, WAFData) => {
  'use strict';

  const SERVICE_URL_TIMEOUT = 5000;
  const PERSON_PRECOND = 'flattenedtaxonomies:"types/Person" AND current:"active"';
  const PERSON_FIELDS = ['[ds6w:identifier]', '[ds6wg:fullname]', '[firstname]', '[lastname]'];

  const getPlatformId = () => widget.getValue('x3dPlatformId');

  let searchUrl;

  const getSearchUrl = () => {
    searchUrl ??= new Promise((resolve, reject) => {
      const fail = () => reject(new Error('3DSearch 주소를 얻지 못했습니다.'));
      _.delay(fail, SERVICE_URL_TIMEOUT);
      CompassServices.getServiceUrl({
        serviceName: '3DSearch',
        platformId: getPlatformId() || undefined,
        onComplete: (result) => {
          const url = _.trimEnd(_.isArray(result) ? _.get(result, '[0].url') : result, '/');
          if (!url) {
            fail();
            return;
          }
          resolve(_.endsWith(url, '/federated') ? url : `${url}/federated`);
        },
        onFailure: fail
      });
    }).catch((error) => {
      searchUrl = undefined;
      throw error;
    });
    return searchUrl;
  };

  const failureReason = (data) => _.find([_.get(data, 'error.message'), _.get(data, 'message'), _.get(data, 'errorMessage'), _.get(data, 'error')], _.isString);

  const postJson = (url, body) => new Promise((resolve, reject) => {
    WAFData.authenticatedRequest(url, {
      method: 'POST',
      type: 'json',
      headers: { Accept: 'application/json', 'Content-Type': 'application/json', 'Accept-Language': widget.lang },
      data: JSON.stringify(body),
      onComplete: resolve,
      onFailure: (error, data) => reject(new Error(_.compact([_.get(error, 'message', error), failureReason(data)]).join(' / ')))
    });
  });

  const personQuery = (text) => {
    const word = `(*${_.trim(text.replace(/["#$@%*,?\<>[\]|:]/g, ' '))}*)`;
    return `[ds6w:type]:"Person" AND (${_.map(PERSON_FIELDS, (field) => `${field}:${word}`).join(' OR ')}) AND policycurrent:"Person.Active"`;
  };

  const sourceError = (res) => {
    const source = _.find(_.get(res, 'infos.sources'), 'error');
    return source && _.compact([source.name, source.error.errorCode, source.error.errorMessage]).join(' ');
  };

  const searchPersons = async (text) => {
    const tenant = getPlatformId() || 'OnPremise';
    const res = await postJson(`${await getSearchUrl()}/search?${new URLSearchParams({ xrequestedwith: 'xmlhttprequest', tenant })}`, {
      label: `XXPersonPicker-${Date.now()}`,
      query: personQuery(text),
      select_predicate: ['ds6w:label', 'ds6w:identifier', 'ds6wg:fullname'],
      order_field: 'relevance',
      order_by: 'desc',
      nresults: 50,
      start: '0',
      source: ['3dspace'],
      tenant,
      locale: widget.lang
    });
    if (_.isEmpty(res.results) && sourceError(res)) {
      throw new Error(sourceError(res));
    }
    return _.map(res.results, ({ attributes }) => {
      const values = _.fromPairs(_.map(attributes, ({ name, value }) => [name, value]));
      return {
        id: values.resourceid,
        identifier: values['ds6w:identifier'],
        name: values['ds6wg:fullname'] || values['ds6w:label'] || values['ds6w:identifier']
      };
    });
  };

  const fromSearchResult = (object) => ({
    id: object.id,
    identifier: object['ds6w:identifier_value'],
    name: object['ds6w:label_value'] || object['ds6w:identifier_value']
  });

  const toItem = (person) => ({ label: `${person.name} (${person.identifier})`, value: person.identifier, grid: person });

  const create = (options = {}) => {
    const multiple = options.multiple ?? true;
    const key = options.key ?? 'id';
    const search = options.search ?? searchPersons;

    const control = new AutoComplete({
      placeholder: options.placeholder ?? '이름이나 아이디 3글자 이상',
      multiSearchMode: multiple,
      allowFreeInputFlag: false,
      keepSearchResultsFlag: false,
      minLengthBeforeSearch: 3,
      typeDelayBeforeSearch: 300,
      filterCB: _.noop,
      elementsTree: async (text) => {
        try {
          const persons = await search(text);
          control.customFilterMessage = emptyMessage;
          return _.map(_.differenceBy(persons, getPersons(), 'id'), toItem);
        } catch (error) {
          console.error('personPicker search failed', error);
          control.customFilterMessage = `검색 실패: ${_.get(error, 'message', error)}`;
          return [];
        }
      }
    });
    const emptyMessage = control.customFilterMessage;

    const getPersons = () => _.map(_.compact(_.castArray(control.selectedItems)), 'options.grid');

    const getValue = () => {
      const values = _.map(getPersons(), key);
      return multiple ? values : (values[0] ?? '');
    };

    const add = (persons) => {
      const list = multiple ? _.uniqBy([...getPersons(), ..._.castArray(persons)], 'id') : _.take(_.castArray(persons), 1);
      control.selectedItems = _.map(list, (person) => new TreeNodeModel(toItem(person)));
    };

    const clear = () => {
      control.selectedItems = [];
    };

    const socketId = `XX_personPicker_${UWA.Utils.getUUID()}`;
    let socket;

    const getSocket = () => {
      if (!socket) {
        socket = SearchCom.createSocket({ socket_id: socketId });
        socket.addListener('Selected_Objects_search', (objects) => add(_.map(objects, fromSearchResult)));
      }
      return socket;
    };

    const openSearch = () => {
      const searchOptions = {
        app_socket_id: socketId,
        widget_id: socketId,
        mode: 'furtive',
        title: options.searchTitle ?? '사람 찾기',
        multiSel: multiple,
        precond: PERSON_PRECOND,
        default_with_precond: true,
        default_search_criteria: '',
        show_precond: false,
        resourceid_not_in: _.map(getPersons(), 'id'),
        source: ['3dspace'],
        tenant: getPlatformId(),
        idcard_activated: false,
        recent_search: { types: ['Person'] }
      };
      const searchSocket = getSocket();
      searchSocket.dispatchEvent('RegisterContext', searchOptions);
      searchSocket.dispatchEvent('InContextSearch', searchOptions);
    };

    const searchButton = new Button({
      displayStyle: 'lite',
      icon: { iconName: 'search', fontIconFamily: WUXManagedFontIcons.Font3DS },
      tooltipInfos: { shortHelp: '3DSearch 에서 찾기' },
      onClick: openSearch
    });
    searchButton.getContent().getElement('.wux-ui-3ds-search').setStyles({ fontSize: '11px', color: '#3d3d3d' });

    control.getContent().setStyle('width', '100%');
    const element = UWA.createElement('div', {
      styles: { display: 'flex', alignItems: 'center', gap: '4px' },
      html: [{ styles: { flex: '1', minWidth: '0' }, html: [control] }, searchButton]
    });

    return { element, control, getValue, add, clear };
  };

  return { create, searchPersons };
});
