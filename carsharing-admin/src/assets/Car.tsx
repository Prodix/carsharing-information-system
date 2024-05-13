import * as React from "react";

function Car({ props }: any) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={32}
      height={32}
      viewBox="0 0 24 24"
      {...props}
    >
      <g fill="none">
        <path d="M24 0v24H0V0zM12.593 23.258l-.011.002-.071.035-.02.004-.014-.004-.071-.035c-.01-.004-.019-.001-.024.005l-.004.01-.017.428.005.02.01.013.104.074.015.004.012-.004.104-.074.012-.016.004-.017-.017-.427c-.002-.01-.009-.017-.017-.018m.265-.113l-.013.002-.185.093-.01.01-.003.011.018.43.005.012.008.007.201.093c.012.004.023 0 .029-.008l.004-.014-.034-.614c-.003-.012-.01-.02-.02-.022m-.715.002a.023.023 0 00-.027.006l-.006.014-.034.614c0 .012.007.02.017.024l.015-.002.201-.093.01-.008.004-.011.017-.43-.003-.012-.01-.01z" />
        <path
          fill="#69c"
          d="M15.764 4a3 3 0 012.683 1.658l1.383 2.765c.244-.1.487-.201.723-.318a1 1 0 01.894 1.79c-.494.246-.72.322-.72.322l.956 1.913c.209.417.317.876.317 1.342V16a2.99 2.99 0 01-1 2.236V19.5a1.5 1.5 0 01-3 0V19H6v.5a1.5 1.5 0 01-3 0v-1.264c-.614-.55-1-1.348-1-2.236v-2.528a3 3 0 01.317-1.341l.956-1.914a14.405 14.405 0 01-.718-.321 1 1 0 01-.45-1.343 1.011 1.011 0 011.347-.445c.235.114.476.217.718.315l1.383-2.765A3 3 0 018.236 4zm3.07 6.904C17.134 11.441 14.715 12 12 12s-5.134-.56-6.834-1.096l-1.06 2.12a1 1 0 00-.106.448V16a1 1 0 001 1h14a1 1 0 001-1v-2.528a1 1 0 00-.106-.447l-1.06-2.12zM7.5 13a1.5 1.5 0 110 3 1.5 1.5 0 010-3m9 0a1.5 1.5 0 110 3 1.5 1.5 0 010-3m-.736-7H8.236a1 1 0 00-.894.553L6.072 9.09C7.62 9.555 9.706 10 12 10s4.38-.445 5.927-.91l-1.269-2.537A1 1 0 0015.764 6"
        />
      </g>
    </svg>
  );
}

export default Car;