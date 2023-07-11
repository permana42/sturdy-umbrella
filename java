javascript:(function(){
  /*! cq-resizer.js 1.0.0 | © 2023 grip on minds | MIT License (https://opensource.org/license/mit/) */
  const createStyles = () => {
    const cssText = `
      dialog {
        --color  : hsl(0 0% 6%);
        --bg     : hsl(170 5% 84%);
        --border : hsl(0 0% 40%);
        color-scheme: light;
        accent-color: auto;
        display: grid;
        border: solid 1px var(--border);
        width: min(35em, 100%);
        height: auto;
        max-height: calc(100dvh - 40px);
        overflow-y: auto;
        position: fixed;
        top: 20px;
        right: 20px;
        left: auto;
        z-index: 2147483647;
        background: var(--bg);
        font-family: sans-serif;
        font-size: 12px;
        line-height: 1.6;
      }
      * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
        color: var(--color);
        font-size: inherit;
        font-weight: normal;
      }
      .header {
        display: grid;
        grid-template-columns: 1fr auto;
        justify-content: space-between;
        align-items: center;
        border-bottom: solid 1px var(--border);
        position: sticky;
        top: 0;
        z-index: 1;
        background: var(--bg);
      }
      h1 {
        padding: 10px 20px;
        border-right: solid 1px var(--border);
      }
      .message {
        padding: 20px;
      }
      ol {
        display: grid;
        gap: 20px;
        padding: 20px;
        counter-set: list-counter;
      }
      li {
        display: grid;
        grid-template-columns: auto 1fr auto auto;
        gap: 20px;
        align-items: center;
        counter-increment: list-counter;
      }
      li::before {
        content: counter(list-counter);
      }
      a {
        color: var(--color);
        text-decoration: none;
        overflow-wrap: anywhere;
      }
      .size {
        display: grid;
        grid-template-columns: repeat(2, auto);
        column-gap: 5px;
        align-content: start;
      }
      input[type="number"] {
        padding-left: 0.5em;
        width: 5em;
        text-align: right;
      }
      button {
        padding: 10px 20px;
        border: none;
        border-radius: 0;
        width: 100%;
        height: 100%;
        display: block;
        background: none;
        appearance: none;
        cursor: pointer;
      }
      @media (hover: hover) and (pointer: fine) {
        a:hover,
        button:hover {
          opacity: 0.7;
        }
      }
    `;
    const styleElem = document.createElement('style');
    styleElem.textContent = cssText;
    return styleElem;
  };

  const createCloseButton = () => {
    const div = document.createElement('div');
    const close = document.createElement('button');
    close.setAttribute('type', 'button');
    close.textContent = 'Close ✕';

    close.addEventListener('click', () => {
      const cqResizer = document.querySelector('cq-resizer');
      cqResizer &&  document.body.removeChild(cqResizer);
    }, false);

    div.append(close);
    return div;
  };

  const setCheckboxEvent = (checkbox, container) => {
    checkbox.addEventListener('change', () => {
      if (checkbox.checked) {
        container.setAttribute('style', container.getAttribute('data-cq-resizer-style'));
      } else {
        container.setAttribute('data-cq-resizer-style', container.getAttribute('style'));
        container.removeAttribute('style');
      }
    });
  };

  const setSizeEvent = (input, container) => {
    input.addEventListener('input', () => {
      if (input.value >= 0) {
        container.style.width = `${input.value}px`;
      }
    });

    const resizeObserver = new ResizeObserver((entries) => {
      for (let entry of entries) {
        input.value = Math.round(entry.borderBoxSize[0].inlineSize);
      }
    });
    resizeObserver.observe(container);
  };

  const createHTML = () => {
    let all = document.querySelectorAll('*');

    const containers = Array.from(all).map((el) => {
      const style = window.getComputedStyle(el);
      return style.container.match(/size/) ? el : false;
    }).filter(Boolean);

    /* `dialog` 要素とヘッダの生成　*/
    const dialog = document.createElement('dialog');
    dialog.open = true;
    dialog.setAttribute('aria-labelledby', 'title');

    const header = document.createElement('div');
    header.classList.add('header');
    const title = document.createElement('h1');
    title.id = 'title';
    title.textContent = 'cq-resizer';
    const closeBtn = createCloseButton();

    header.append(title);
    header.append(closeBtn);
    dialog.append(header);

    /* `container` が見つからない場合にメッセージを返す */
    if (containers.length < 1) {
      const message = document.createElement('p');
      message.textContent = 'container が指定されている要素は見つかりませんでした';
      message.classList.add('message');
      dialog.append(message);
      return dialog;
    }

    /* 右辺が同じ XY 座標にあるコンテナを除外 */
    const filteredContainers = containers.map((el) => {
      const isSamePosition = containers.some((parent) => {
        if (parent === el) return false;

        const parentRect = parent.getBoundingClientRect();
        const elRect = el.getBoundingClientRect();

        if (
          parent.contains(el) && 
          (parentRect.right === elRect.right) &&
          (parentRect.bottom === elRect.bottom)
        ) {
          return true;
        } else {
          return false;
        }
      });

      if (isSamePosition) return false;
      return el;
    }).filter(Boolean);

    /* 要素の生成 */
    const ol = document.createElement('ol');
    const fragment = document.createDocumentFragment();

    filteredContainers.forEach((container, index) => {
      const style = window.getComputedStyle(container);

      /* コンテナにリサイズハンドルを追加 */
      if (style.container.includes('inline-size')) {
        container.style.resize = 'horizontal';
        container.style.overflow = 'auto';
      } else if (style.container.includes('size')) {
        container.style.resize = 'both';
        container.style.overflow = 'auto';
      }

      const li = document.createElement('li');
      const a = document.createElement('a');
      const checkbox = document.createElement('input');

      const createLinkText = (container) => {
        const array = Array.from(container.classList);

        if (array.length < 1) {
          return `<${container.tagName.toLowerCase()}>`;
        }
        return `.${array.join('.')}`;
      };

      /* アンカーリンクの生成 */
      const id = 'cq-resizer-' + (index + 1);
      a.href = `#${id}`;
      a.textContent = createLinkText(container);

      const anchor = document.createElement('span');
      anchor.setAttribute('id', id);
      anchor.setAttribute('aria-hidden', 'true');
      anchor.style.cssText = `
        margin: -1px;
        padding: 0;
        border-width: 0;
        position: absolute;
        top: 0;
        width: 1px;
        height: 1px;
        overflow: hidden;
        clip: rect(0, 0, 0, 0);
        white-space: nowrap;
      `;
      container.append(anchor);

      /* サイズ表示の生成 */
      const size = document.createElement('div');
      size.classList.add('size');
      size.textContent = 'px';
      const number = document.createElement('input');
      number.setAttribute('type', 'number');
      number.value = container.style.width;
      size.prepend(number);

      setSizeEvent(number, container);

      /* チェックボックスの生成 */
      checkbox.setAttribute('type', 'checkbox');
      checkbox.checked = true;

      setCheckboxEvent(checkbox, container);

      li.append(a);
      li.append(size);
      li.append(checkbox);
      fragment.append(li);
    });

    ol.append(fragment);
    dialog.append(ol);
    return dialog;
  };

  if (!customElements.get('cq-resizer')) {
    const styles = createStyles();
    const html = createHTML();

    class CqResizer extends HTMLElement {
      constructor() {
        super();

        const shadow = this.attachShadow({ mode: 'open' });
        shadow.append(styles);
        shadow.append(html);
      }
    }
    customElements.define('cq-resizer', CqResizer);
  }
  const cqResizer = document.createElement('cq-resizer');
  document.body.prepend(cqResizer);
})();
