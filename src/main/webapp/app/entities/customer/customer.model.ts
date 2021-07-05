export interface ICustomer {
  id?: number;
  userId?: number | null;
  companyName?: string | null;
}

export class Customer implements ICustomer {
  constructor(public id?: number, public userId?: number | null, public companyName?: string | null) {}
}

export function getCustomerIdentifier(customer: ICustomer): number | undefined {
  return customer.id;
}
