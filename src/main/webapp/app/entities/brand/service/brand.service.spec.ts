import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IBrand, Brand } from '../brand.model';

import { BrandService } from './brand.service';

describe('Service Tests', () => {
  describe('Brand Service', () => {
    let service: BrandService;
    let httpMock: HttpTestingController;
    let elemDefault: IBrand;
    let expectedResult: IBrand | IBrand[] | boolean | null;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
      });
      expectedResult = null;
      service = TestBed.inject(BrandService);
      httpMock = TestBed.inject(HttpTestingController);

      elemDefault = {
        id: 0,
        brandId: 0,
        brandName: 'AAAAAAA',
      };
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign({}, elemDefault);

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(elemDefault);
      });

      it('should create a Brand', () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.create(new Brand()).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should update a Brand', () => {
        const returnedFromService = Object.assign(
          {
            id: 1,
            brandId: 1,
            brandName: 'BBBBBB',
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.update(expected).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should partial update a Brand', () => {
        const patchObject = Object.assign(
          {
            brandId: 1,
          },
          new Brand()
        );

        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = Object.assign({}, returnedFromService);

        service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PATCH' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should return a list of Brand', () => {
        const returnedFromService = Object.assign(
          {
            id: 1,
            brandId: 1,
            brandName: 'BBBBBB',
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.query().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a Brand', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });

      describe('addBrandToCollectionIfMissing', () => {
        it('should add a Brand to an empty array', () => {
          const brand: IBrand = { id: 123 };
          expectedResult = service.addBrandToCollectionIfMissing([], brand);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(brand);
        });

        it('should not add a Brand to an array that contains it', () => {
          const brand: IBrand = { id: 123 };
          const brandCollection: IBrand[] = [
            {
              ...brand,
            },
            { id: 456 },
          ];
          expectedResult = service.addBrandToCollectionIfMissing(brandCollection, brand);
          expect(expectedResult).toHaveLength(2);
        });

        it("should add a Brand to an array that doesn't contain it", () => {
          const brand: IBrand = { id: 123 };
          const brandCollection: IBrand[] = [{ id: 456 }];
          expectedResult = service.addBrandToCollectionIfMissing(brandCollection, brand);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(brand);
        });

        it('should add only unique Brand to an array', () => {
          const brandArray: IBrand[] = [{ id: 123 }, { id: 456 }, { id: 63528 }];
          const brandCollection: IBrand[] = [{ id: 123 }];
          expectedResult = service.addBrandToCollectionIfMissing(brandCollection, ...brandArray);
          expect(expectedResult).toHaveLength(3);
        });

        it('should accept varargs', () => {
          const brand: IBrand = { id: 123 };
          const brand2: IBrand = { id: 456 };
          expectedResult = service.addBrandToCollectionIfMissing([], brand, brand2);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(brand);
          expect(expectedResult).toContain(brand2);
        });

        it('should accept null and undefined values', () => {
          const brand: IBrand = { id: 123 };
          expectedResult = service.addBrandToCollectionIfMissing([], null, brand, undefined);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(brand);
        });
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
