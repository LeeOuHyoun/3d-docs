define('DS/CommonUtils/seedWidgetLayout/seedWidgetLayoutDemo', [
  'UWA/Core',
  'DS/CommonUtils/seedWidgetLayout/seedWidgetLayout',
  'UWA/Element'
], (UWA, seedWidgetLayout) => {
  'use strict';

  const page = (text) => (content) => {
    UWA.createElement('div', { text }).inject(content);
  };

  const SECTIONS = [
    {
      id: 'view',
      title: '조회',
      actions: [
        { id: 'recent', text: '최근 항목', fonticon: 'clock', render: page('최근 항목 화면') },
        { id: 'myItems', text: '내 항목', fonticon: 'user', render: page('내 항목 화면') },
        { id: 'allItems', text: '전체 항목', fonticon: 'list', render: page('전체 항목 화면') }
      ]
    },
    {
      id: 'start',
      title: '작업 시작',
      actions: [
        { id: 'newItem', text: '새 항목', fonticon: 'plus', render: page('새 항목 화면') }
      ]
    }
  ];

  const render = (parent) => seedWidgetLayout.create({
    container: parent,
    title: 'Seed Widget',
    description: '왼쪽 메뉴를 누르면 가운데 화면이 바뀝니다.',
    sections: SECTIONS,
    selectedId: 'recent'
  });

  return { render };
});
