export interface IBrand {
  id?: number;
  brandId?: number | null;
  brandName?: string | null;
}

export class Brand implements IBrand {
  constructor(public id?: number, public brandId?: number | null, public brandName?: string | null) {}
}

export function getBrandIdentifier(brand: IBrand): number | undefined {
  return brand.id;
}
