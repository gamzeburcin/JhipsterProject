jest.mock('@angular/router');

import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { IColor, Color } from '../color.model';
import { ColorService } from '../service/color.service';

import { ColorRoutingResolveService } from './color-routing-resolve.service';

describe('Service Tests', () => {
  describe('Color routing resolve service', () => {
    let mockRouter: Router;
    let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
    let routingResolveService: ColorRoutingResolveService;
    let service: ColorService;
    let resultColor: IColor | undefined;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [Router, ActivatedRouteSnapshot],
      });
      mockRouter = TestBed.inject(Router);
      mockActivatedRouteSnapshot = TestBed.inject(ActivatedRouteSnapshot);
      routingResolveService = TestBed.inject(ColorRoutingResolveService);
      service = TestBed.inject(ColorService);
      resultColor = undefined;
    });

    describe('resolve', () => {
      it('should return IColor returned by find', () => {
        // GIVEN
        service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultColor = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultColor).toEqual({ id: 123 });
      });

      it('should return new IColor if id is not provided', () => {
        // GIVEN
        service.find = jest.fn();
        mockActivatedRouteSnapshot.params = {};

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultColor = result;
        });

        // THEN
        expect(service.find).not.toBeCalled();
        expect(resultColor).toEqual(new Color());
      });

      it('should route to 404 page if data not found in server', () => {
        // GIVEN
        spyOn(service, 'find').and.returnValue(of(new HttpResponse({ body: null })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultColor = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultColor).toEqual(undefined);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
      });
    });
  });
});
