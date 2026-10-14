import React from "react";

type CardProps = React.HTMLAttributes<HTMLDivElement> & { quiet?: boolean };

export const Card: React.FC<CardProps> = ({ quiet = false, children, className = "", ...rest }) => {
  return (
    <div className={(quiet ? "card card-quiet " : "card ") + className} {...rest}>
      {children}
    </div>
  );
};

export default Card;
