jest.mock('@angular/router');

import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { IRental, Rental } from '../rental.model';
import { RentalService } from '../service/rental.service';

import { RentalRoutingResolveService } from './rental-routing-resolve.service';

describe('Service Tests', () => {
  describe('Rental routing resolve service', () => {
    let mockRouter: Router;
    let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
    let routingResolveService: RentalRoutingResolveService;
    let service: RentalService;
    let resultRental: IRental | undefined;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [Router, ActivatedRouteSnapshot],
      });
      mockRouter = TestBed.inject(Router);
      mockActivatedRouteSnapshot = TestBed.inject(ActivatedRouteSnapshot);
      routingResolveService = TestBed.inject(RentalRoutingResolveService);
      service = TestBed.inject(RentalService);
      resultRental = undefined;
    });

    describe('resolve', () => {
      it('should return IRental returned by find', () => {
        // GIVEN
        service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultRental = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultRental).toEqual({ id: 123 });
      });

      it('should return new IRental if id is not provided', () => {
        // GIVEN
        service.find = jest.fn();
        mockActivatedRouteSnapshot.params = {};

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultRental = result;
        });

        // THEN
        expect(service.find).not.toBeCalled();
        expect(resultRental).toEqual(new Rental());
      });

      it('should route to 404 page if data not found in server', () => {
        // GIVEN
        spyOn(service, 'find').and.returnValue(of(new HttpResponse({ body: null })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultRental = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultRental).toEqual(undefined);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
      });
    });
  });
});
