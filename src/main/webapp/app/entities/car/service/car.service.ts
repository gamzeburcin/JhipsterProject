import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { ICar, getCarIdentifier } from '../car.model';

export type EntityResponseType = HttpResponse<ICar>;
export type EntityArrayResponseType = HttpResponse<ICar[]>;

@Injectable({ providedIn: 'root' })
export class CarService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/cars');
  public resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/_search/cars');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(car: ICar): Observable<EntityResponseType> {
    return this.http.post<ICar>(this.resourceUrl, car, { observe: 'response' });
  }

  update(car: ICar): Observable<EntityResponseType> {
    return this.http.put<ICar>(`${this.resourceUrl}/${getCarIdentifier(car) as number}`, car, { observe: 'response' });
  }

  partialUpdate(car: ICar): Observable<EntityResponseType> {
    return this.http.patch<ICar>(`${this.resourceUrl}/${getCarIdentifier(car) as number}`, car, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ICar>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ICar[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ICar[]>(this.resourceSearchUrl, { params: options, observe: 'response' });
  }

  addCarToCollectionIfMissing(carCollection: ICar[], ...carsToCheck: (ICar | null | undefined)[]): ICar[] {
    const cars: ICar[] = carsToCheck.filter(isPresent);
    if (cars.length > 0) {
      const carCollectionIdentifiers = carCollection.map(carItem => getCarIdentifier(carItem)!);
      const carsToAdd = cars.filter(carItem => {
        const carIdentifier = getCarIdentifier(carItem);
        if (carIdentifier == null || carCollectionIdentifiers.includes(carIdentifier)) {
          return false;
        }
        carCollectionIdentifiers.push(carIdentifier);
        return true;
      });
      return [...carsToAdd, ...carCollection];
    }
    return carCollection;
  }
}
