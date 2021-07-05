export interface IPayment {
  id?: number;
  amount?: number | null;
}

export class Payment implements IPayment {
  constructor(public id?: number, public amount?: number | null) {}
}

export function getPaymentIdentifier(payment: IPayment): number | undefined {
  return payment.id;
}
