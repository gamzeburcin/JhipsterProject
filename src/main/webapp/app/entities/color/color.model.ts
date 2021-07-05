export interface IColor {
  id?: number;
  colorName?: string | null;
}

export class Color implements IColor {
  constructor(public id?: number, public colorName?: string | null) {}
}

export function getColorIdentifier(color: IColor): number | undefined {
  return color.id;
}
