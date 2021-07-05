import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IRental, Rental } from '../rental.model';
import { RentalService } from '../service/rental.service';

@Injectable({ providedIn: 'root' })
export class RentalRoutingResolveService implements Resolve<IRental> {
  constructor(protected service: RentalService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IRental> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((rental: HttpResponse<Rental>) => {
          if (rental.body) {
            return of(rental.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Rental());
  }
}
