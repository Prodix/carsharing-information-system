import * as React from "react";

function Exit({ props }: any) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={32}
      height={32}
      viewBox="0 0 24 24"
      {...props}
    >
      <path
        fill="none"
        stroke="#69c"
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M16 4h3a2 2 0 012 2v1m-5 13h3a2 2 0 002-2v-1M4.425 19.428l6 1.8A2 2 0 0013 19.312V4.688a2 2 0 00-2.575-1.916l-6 1.8A2 2 0 003 6.488v11.024a2 2 0 001.425 1.916M16.001 12h5m0 0l-2-2m2 2l-2 2"
      />
    </svg>
  );
}

export default Exit;
