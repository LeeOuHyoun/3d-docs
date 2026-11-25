define('DS/Recipes/personPicker/personPicker', [
  'DS/lodash-4.18.1/lodash',
  'DS/WUXAutoComplete/AutoComplete',
  'DS/TreeModel/TreeNodeModel',
  'DS/WAFData/WAFData',
  'DS/WebappsUtils/WebappsUtils'
], (_, AutoComplete, TreeNodeModel, WAFData, WebappsUtils) => {
  'use strict';

  const SPACE_URL = WebappsUtils.getWebappsBaseUrl().replace(/webapps\/?$/, '');

  const getJson = (path, params, headers) => new Promise((resolve, reject) => {
    const query = new URLSearchParams({ tenant: widget.getValue('x3dPlatformId') || 'OnPremise', ...params });
    WAFData.authenticatedRequest(`${SPACE_URL}${path}?${query}`, {
      type: 'json',
      headers: { Accept: 'application/json', ...headers },
      onComplete: resolve,
      onFailure: reject
    });
  });

  let securityContext;

  const getSecurityContext = () => {
    securityContext ??= getJson('resources/pno/person/getsecuritycontext')
      .then(({ SecurityContext: sc }) => (_.startsWith(sc, 'ctx::') ? sc : `ctx::${sc}`))
      .catch((error) => {
        securityContext = undefined;
        throw error;
      });
    return securityContext;
  };

  const searchPersons = async (text) => {
    const sc = await getSecurityContext();
    const res = await getJson('resources/v2/e6w/service/PersonSearch', { searchStr: `${text}*` }, { 'Accept-Language': widget.lang, SecurityContext: sc });
    return _.map(_.filter(res.data, 'dataelements'), ({ id, dataelements: de }) => ({
      id,
      identifier: de.name,
      name: _.compact([de.firstname, de.lastname]).join(' ') || de.name
    }));
  };

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
      elementsTree: async (text) => _.map(_.differenceBy(await search(text), getPersons(), 'id'), toItem)
    });

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

    return { control, getValue, add, clear };
  };

  return { create, searchPersons };
});
