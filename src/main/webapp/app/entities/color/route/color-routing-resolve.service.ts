import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IColor, Color } from '../color.model';
import { ColorService } from '../service/color.service';

@Injectable({ providedIn: 'root' })
export class ColorRoutingResolveService implements Resolve<IColor> {
  constructor(protected service: ColorService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IColor> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((color: HttpResponse<Color>) => {
          if (color.body) {
            return of(color.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Color());
  }
}
