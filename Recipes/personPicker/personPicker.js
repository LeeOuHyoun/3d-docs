define('DS/Recipes/personPicker/personPicker', [
  'UWA/Core',
  'DS/lodash-4.18.1/lodash',
  'DS/WUXAutoComplete/AutoComplete',
  'DS/TreeModel/TreeNodeModel',
  'DS/Controls/Button',
  'DS/WAFData/WAFData',
  'DS/WebappsUtils/WebappsUtils',
  'DS/PlatformAPI/PlatformAPI',
  'UWA/Element'
], (UWA, _, AutoComplete, TreeNodeModel, Button, WAFData, WebappsUtils, PlatformAPI) => {
  'use strict';

  const TOPIC = 'xx.person.add';

  const toItem = (person) => ({ label: `${person.name} (${person.identifier})`, value: person.identifier, grid: person });

  const create = (options) => {
    const control = new AutoComplete({
      placeholder: options.placeholder ?? '이름이나 아이디 3글자 이상',
      multiSearchMode: true,
      allowFreeInputFlag: false,
      keepSearchResultsFlag: false,
      minLengthBeforeSearch: 3,
      typeDelayBeforeSearch: 300,
      filterCB: _.noop,
      elementsTree: (text) => options.search(text).then((persons) => _.map(_.differenceBy(persons, getPersons(), 'id'), toItem))
    }).inject(options.container);

    const getPersons = () => _.map(control.selectedItems, 'options.grid');

    const getIds = (key = 'id') => _.map(getPersons(), key);

    const add = (persons) => {
      const fresh = _.differenceBy(_.uniqBy(_.castArray(persons), 'id'), getPersons(), 'id');
      if (!_.isEmpty(fresh)) {
        control.selectedItems = _.concat(_.toArray(control.selectedItems), _.map(fresh, (person) => new TreeNodeModel(toItem(person))));
      }
    };

    const clear = () => {
      control.selectedItems = [];
    };

    return { control, add, clear, getPersons, getIds };
  };

  const tenant = () => widget.getValue('x3dPlatformId') || 'OnPremise';

  const request = (path, headers) => new Promise((resolve, reject) => {
    const url = `${WebappsUtils.getWebappsBaseUrl().replace(/webapps\/?$/, '')}${path}${_.includes(path, '?') ? '&' : '?'}tenant=${encodeURIComponent(tenant())}`;
    WAFData.authenticatedRequest(url, {
      type: 'json',
      headers: { Accept: 'application/json', ...headers },
      onComplete: resolve,
      onFailure: reject
    });
  });

  const getSecurityContext = _.once(() => request('resources/pno/person/getsecuritycontext')
    .then(({ SecurityContext: sc }) => (_.startsWith(sc, 'ctx::') ? sc : `ctx::${sc}`)));

  const searchPersons = (text) => getSecurityContext()
    .then((securityContext) => request(`resources/v2/e6w/service/PersonSearch?searchStr=${encodeURIComponent(text)}*`, { 'Accept-Language': widget.lang, SecurityContext: securityContext }))
    .then((res) => _.map(_.filter(res.data, 'dataelements'), ({ id, dataelements: de }) => ({
      id,
      identifier: de.name,
      name: _.trim(`${de.firstname ?? ''} ${de.lastname ?? ''}`) || de.name
    })));

  const render = (parent) => {
    const page = UWA.createElement('div', { styles: { padding: '12px' } }).inject(parent);
    const field = UWA.createElement('div', { styles: { width: '480px' } }).inject(page);
    const actions = UWA.createElement('div', { styles: { display: 'flex', gap: '8px', margin: '8px 0' } }).inject(page);
    const output = UWA.createElement('pre').inject(page);
    const picker = create({ container: field, search: searchPersons });
    const show = () => output.setText(JSON.stringify({ id: picker.getIds(), identifier: picker.getIds('identifier') }, null, 2));

    picker.control.addEventListener('change', show);
    PlatformAPI.subscribe(TOPIC, picker.add);

    new Button({ label: '다른 위젯에서 보내기 예', onClick: () => PlatformAPI.publish(TOPIC, [{ id: 'SAMPLE-PHYSICALID', identifier: 'sample01', name: '예시 사용자' }]) }).inject(actions);
    new Button({ label: '모두 지우기', onClick: picker.clear }).inject(actions);
    show();
  };

  return { create, searchPersons, render, TOPIC };
});
