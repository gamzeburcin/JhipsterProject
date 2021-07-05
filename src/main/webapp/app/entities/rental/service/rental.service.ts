import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as dayjs from 'dayjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { IRental, getRentalIdentifier } from '../rental.model';

export type EntityResponseType = HttpResponse<IRental>;
export type EntityArrayResponseType = HttpResponse<IRental[]>;

@Injectable({ providedIn: 'root' })
export class RentalService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/rentals');
  public resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/_search/rentals');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(rental: IRental): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(rental);
    return this.http
      .post<IRental>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(rental: IRental): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(rental);
    return this.http
      .put<IRental>(`${this.resourceUrl}/${getRentalIdentifier(rental) as number}`, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  partialUpdate(rental: IRental): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(rental);
    return this.http
      .patch<IRental>(`${this.resourceUrl}/${getRentalIdentifier(rental) as number}`, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IRental>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IRental[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IRental[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  addRentalToCollectionIfMissing(rentalCollection: IRental[], ...rentalsToCheck: (IRental | null | undefined)[]): IRental[] {
    const rentals: IRental[] = rentalsToCheck.filter(isPresent);
    if (rentals.length > 0) {
      const rentalCollectionIdentifiers = rentalCollection.map(rentalItem => getRentalIdentifier(rentalItem)!);
      const rentalsToAdd = rentals.filter(rentalItem => {
        const rentalIdentifier = getRentalIdentifier(rentalItem);
        if (rentalIdentifier == null || rentalCollectionIdentifiers.includes(rentalIdentifier)) {
          return false;
        }
        rentalCollectionIdentifiers.push(rentalIdentifier);
        return true;
      });
      return [...rentalsToAdd, ...rentalCollection];
    }
    return rentalCollection;
  }

  protected convertDateFromClient(rental: IRental): IRental {
    return Object.assign({}, rental, {
      rentDate: rental.rentDate?.isValid() ? rental.rentDate.toJSON() : undefined,
      returnDate: rental.returnDate?.isValid() ? rental.returnDate.toJSON() : undefined,
    });
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.rentDate = res.body.rentDate ? dayjs(res.body.rentDate) : undefined;
      res.body.returnDate = res.body.returnDate ? dayjs(res.body.returnDate) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((rental: IRental) => {
        rental.rentDate = rental.rentDate ? dayjs(rental.rentDate) : undefined;
        rental.returnDate = rental.returnDate ? dayjs(rental.returnDate) : undefined;
      });
    }
    return res;
  }
}
