import * as dayjs from 'dayjs';

export interface ICarImage {
  id?: number;
  carId?: number | null;
  imagePath?: string | null;
  date?: dayjs.Dayjs | null;
}

export class CarImage implements ICarImage {
  constructor(public id?: number, public carId?: number | null, public imagePath?: string | null, public date?: dayjs.Dayjs | null) {}
}

export function getCarImageIdentifier(carImage: ICarImage): number | undefined {
  return carImage.id;
}
