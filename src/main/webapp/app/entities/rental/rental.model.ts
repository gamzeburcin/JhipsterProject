import * as dayjs from 'dayjs';

export interface IRental {
  id?: number;
  rentDate?: dayjs.Dayjs | null;
  returnDate?: dayjs.Dayjs | null;
  customerId?: number | null;
  carId?: number | null;
}

export class Rental implements IRental {
  constructor(
    public id?: number,
    public rentDate?: dayjs.Dayjs | null,
    public returnDate?: dayjs.Dayjs | null,
    public customerId?: number | null,
    public carId?: number | null
  ) {}
}

export function getRentalIdentifier(rental: IRental): number | undefined {
  return rental.id;
}
