import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { IColor, getColorIdentifier } from '../color.model';

export type EntityResponseType = HttpResponse<IColor>;
export type EntityArrayResponseType = HttpResponse<IColor[]>;

@Injectable({ providedIn: 'root' })
export class ColorService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/colors');
  public resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/_search/colors');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(color: IColor): Observable<EntityResponseType> {
    return this.http.post<IColor>(this.resourceUrl, color, { observe: 'response' });
  }

  update(color: IColor): Observable<EntityResponseType> {
    return this.http.put<IColor>(`${this.resourceUrl}/${getColorIdentifier(color) as number}`, color, { observe: 'response' });
  }

  partialUpdate(color: IColor): Observable<EntityResponseType> {
    return this.http.patch<IColor>(`${this.resourceUrl}/${getColorIdentifier(color) as number}`, color, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IColor>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IColor[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IColor[]>(this.resourceSearchUrl, { params: options, observe: 'response' });
  }

  addColorToCollectionIfMissing(colorCollection: IColor[], ...colorsToCheck: (IColor | null | undefined)[]): IColor[] {
    const colors: IColor[] = colorsToCheck.filter(isPresent);
    if (colors.length > 0) {
      const colorCollectionIdentifiers = colorCollection.map(colorItem => getColorIdentifier(colorItem)!);
      const colorsToAdd = colors.filter(colorItem => {
        const colorIdentifier = getColorIdentifier(colorItem);
        if (colorIdentifier == null || colorCollectionIdentifiers.includes(colorIdentifier)) {
          return false;
        }
        colorCollectionIdentifiers.push(colorIdentifier);
        return true;
      });
      return [...colorsToAdd, ...colorCollection];
    }
    return colorCollection;
  }
}
