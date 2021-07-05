export interface ICar {
  id?: number;
  brandId?: number | null;
  colorId?: number | null;
  modelYear?: string | null;
  dailyPrice?: number | null;
  description?: string | null;
}

export class Car implements ICar {
  constructor(
    public id?: number,
    public brandId?: number | null,
    public colorId?: number | null,
    public modelYear?: string | null,
    public dailyPrice?: number | null,
    public description?: string | null
  ) {}
}

export function getCarIdentifier(car: ICar): number | undefined {
  return car.id;
}
