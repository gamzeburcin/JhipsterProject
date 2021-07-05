jest.mock('@angular/router');

import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { IBrand, Brand } from '../brand.model';
import { BrandService } from '../service/brand.service';

import { BrandRoutingResolveService } from './brand-routing-resolve.service';

describe('Service Tests', () => {
  describe('Brand routing resolve service', () => {
    let mockRouter: Router;
    let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
    let routingResolveService: BrandRoutingResolveService;
    let service: BrandService;
    let resultBrand: IBrand | undefined;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [Router, ActivatedRouteSnapshot],
      });
      mockRouter = TestBed.inject(Router);
      mockActivatedRouteSnapshot = TestBed.inject(ActivatedRouteSnapshot);
      routingResolveService = TestBed.inject(BrandRoutingResolveService);
      service = TestBed.inject(BrandService);
      resultBrand = undefined;
    });

    describe('resolve', () => {
      it('should return IBrand returned by find', () => {
        // GIVEN
        service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultBrand = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultBrand).toEqual({ id: 123 });
      });

      it('should return new IBrand if id is not provided', () => {
        // GIVEN
        service.find = jest.fn();
        mockActivatedRouteSnapshot.params = {};

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultBrand = result;
        });

        // THEN
        expect(service.find).not.toBeCalled();
        expect(resultBrand).toEqual(new Brand());
      });

      it('should route to 404 page if data not found in server', () => {
        // GIVEN
        spyOn(service, 'find').and.returnValue(of(new HttpResponse({ body: null })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultBrand = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultBrand).toEqual(undefined);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
      });
    });
  });
});
