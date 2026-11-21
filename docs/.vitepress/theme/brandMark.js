export const markViewBox = '130 88 956 504'

export const markShapes = (id) =>
  `<defs><mask id="${id}-half" maskUnits="userSpaceOnUse" x="0" y="0" width="1300" height="700"><path d="M600 222C555 182 470 132 375 129C260 129 166 223 166 340C166 455 262 548 380 548C450 548 510 525 560 493L784 324" fill="none" stroke="#fff" stroke-width="72"/><polygon points="868,242 722,292 822,382" fill="#fff"/><polygon points="446,239 820,-21 906,102 532,362" fill="#000"/></mask></defs><rect width="1300" height="700" fill="currentColor" mask="url(#${id}-half)"/><rect width="1300" height="700" fill="currentColor" mask="url(#${id}-half)" transform="rotate(180 607 340)"/>`

export const markImage = `url("data:image/svg+xml,${encodeURIComponent(
  `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${markViewBox}">${markShapes('mark')}</svg>`,
)}")`
