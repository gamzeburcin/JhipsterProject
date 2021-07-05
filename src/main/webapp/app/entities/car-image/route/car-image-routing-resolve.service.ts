import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ICarImage, CarImage } from '../car-image.model';
import { CarImageService } from '../service/car-image.service';

@Injectable({ providedIn: 'root' })
export class CarImageRoutingResolveService implements Resolve<ICarImage> {
  constructor(protected service: CarImageService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ICarImage> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((carImage: HttpResponse<CarImage>) => {
          if (carImage.body) {
            return of(carImage.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new CarImage());
  }
}
