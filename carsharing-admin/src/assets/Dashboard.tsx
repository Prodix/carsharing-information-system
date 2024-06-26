import * as React from "react";

function Dashboard({ props }: any) {
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
        d="M2 5a2 2 0 012-2h6v18H4a2 2 0 01-2-2zm12-2h6a2 2 0 012 2v5h-8zm0 11h8v5a2 2 0 01-2 2h-6z"
      />
    </svg>
  );
}

export default Dashboard;
