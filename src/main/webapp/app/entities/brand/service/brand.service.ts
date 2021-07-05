import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { IBrand, getBrandIdentifier } from '../brand.model';

export type EntityResponseType = HttpResponse<IBrand>;
export type EntityArrayResponseType = HttpResponse<IBrand[]>;

@Injectable({ providedIn: 'root' })
export class BrandService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/brands');
  public resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/_search/brands');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(brand: IBrand): Observable<EntityResponseType> {
    return this.http.post<IBrand>(this.resourceUrl, brand, { observe: 'response' });
  }

  update(brand: IBrand): Observable<EntityResponseType> {
    return this.http.put<IBrand>(`${this.resourceUrl}/${getBrandIdentifier(brand) as number}`, brand, { observe: 'response' });
  }

  partialUpdate(brand: IBrand): Observable<EntityResponseType> {
    return this.http.patch<IBrand>(`${this.resourceUrl}/${getBrandIdentifier(brand) as number}`, brand, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IBrand>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IBrand[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IBrand[]>(this.resourceSearchUrl, { params: options, observe: 'response' });
  }

  addBrandToCollectionIfMissing(brandCollection: IBrand[], ...brandsToCheck: (IBrand | null | undefined)[]): IBrand[] {
    const brands: IBrand[] = brandsToCheck.filter(isPresent);
    if (brands.length > 0) {
      const brandCollectionIdentifiers = brandCollection.map(brandItem => getBrandIdentifier(brandItem)!);
      const brandsToAdd = brands.filter(brandItem => {
        const brandIdentifier = getBrandIdentifier(brandItem);
        if (brandIdentifier == null || brandCollectionIdentifiers.includes(brandIdentifier)) {
          return false;
        }
        brandCollectionIdentifiers.push(brandIdentifier);
        return true;
      });
      return [...brandsToAdd, ...brandCollection];
    }
    return brandCollection;
  }
}
