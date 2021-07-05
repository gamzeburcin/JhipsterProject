import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as dayjs from 'dayjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { ICarImage, getCarImageIdentifier } from '../car-image.model';

export type EntityResponseType = HttpResponse<ICarImage>;
export type EntityArrayResponseType = HttpResponse<ICarImage[]>;

@Injectable({ providedIn: 'root' })
export class CarImageService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/car-images');
  public resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/_search/car-images');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(carImage: ICarImage): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(carImage);
    return this.http
      .post<ICarImage>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(carImage: ICarImage): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(carImage);
    return this.http
      .put<ICarImage>(`${this.resourceUrl}/${getCarImageIdentifier(carImage) as number}`, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  partialUpdate(carImage: ICarImage): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(carImage);
    return this.http
      .patch<ICarImage>(`${this.resourceUrl}/${getCarImageIdentifier(carImage) as number}`, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<ICarImage>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ICarImage[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ICarImage[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  addCarImageToCollectionIfMissing(carImageCollection: ICarImage[], ...carImagesToCheck: (ICarImage | null | undefined)[]): ICarImage[] {
    const carImages: ICarImage[] = carImagesToCheck.filter(isPresent);
    if (carImages.length > 0) {
      const carImageCollectionIdentifiers = carImageCollection.map(carImageItem => getCarImageIdentifier(carImageItem)!);
      const carImagesToAdd = carImages.filter(carImageItem => {
        const carImageIdentifier = getCarImageIdentifier(carImageItem);
        if (carImageIdentifier == null || carImageCollectionIdentifiers.includes(carImageIdentifier)) {
          return false;
        }
        carImageCollectionIdentifiers.push(carImageIdentifier);
        return true;
      });
      return [...carImagesToAdd, ...carImageCollection];
    }
    return carImageCollection;
  }

  protected convertDateFromClient(carImage: ICarImage): ICarImage {
    return Object.assign({}, carImage, {
      date: carImage.date?.isValid() ? carImage.date.toJSON() : undefined,
    });
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.date = res.body.date ? dayjs(res.body.date) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((carImage: ICarImage) => {
        carImage.date = carImage.date ? dayjs(carImage.date) : undefined;
      });
    }
    return res;
  }
}
