import React from "react";

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & { variant?: "primary" | "ghost" };

export const Button: React.FC<ButtonProps> = ({ variant = "primary", children, className = "", ...rest }) => {
  const base = "btn ";
  const v = variant === "primary" ? "btn-primary" : "btn-ghost";
  return (
    <button className={base + v + " " + className} {...rest}>
      {children}
    </button>
  );
};

export default Button;
