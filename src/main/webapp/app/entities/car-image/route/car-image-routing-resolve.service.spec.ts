jest.mock('@angular/router');

import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { ICarImage, CarImage } from '../car-image.model';
import { CarImageService } from '../service/car-image.service';

import { CarImageRoutingResolveService } from './car-image-routing-resolve.service';

describe('Service Tests', () => {
  describe('CarImage routing resolve service', () => {
    let mockRouter: Router;
    let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
    let routingResolveService: CarImageRoutingResolveService;
    let service: CarImageService;
    let resultCarImage: ICarImage | undefined;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [Router, ActivatedRouteSnapshot],
      });
      mockRouter = TestBed.inject(Router);
      mockActivatedRouteSnapshot = TestBed.inject(ActivatedRouteSnapshot);
      routingResolveService = TestBed.inject(CarImageRoutingResolveService);
      service = TestBed.inject(CarImageService);
      resultCarImage = undefined;
    });

    describe('resolve', () => {
      it('should return ICarImage returned by find', () => {
        // GIVEN
        service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultCarImage = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultCarImage).toEqual({ id: 123 });
      });

      it('should return new ICarImage if id is not provided', () => {
        // GIVEN
        service.find = jest.fn();
        mockActivatedRouteSnapshot.params = {};

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultCarImage = result;
        });

        // THEN
        expect(service.find).not.toBeCalled();
        expect(resultCarImage).toEqual(new CarImage());
      });

      it('should route to 404 page if data not found in server', () => {
        // GIVEN
        spyOn(service, 'find').and.returnValue(of(new HttpResponse({ body: null })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultCarImage = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultCarImage).toEqual(undefined);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
      });
    });
  });
});
