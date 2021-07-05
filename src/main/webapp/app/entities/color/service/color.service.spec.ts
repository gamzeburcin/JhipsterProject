import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IColor, Color } from '../color.model';

import { ColorService } from './color.service';

describe('Service Tests', () => {
  describe('Color Service', () => {
    let service: ColorService;
    let httpMock: HttpTestingController;
    let elemDefault: IColor;
    let expectedResult: IColor | IColor[] | boolean | null;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
      });
      expectedResult = null;
      service = TestBed.inject(ColorService);
      httpMock = TestBed.inject(HttpTestingController);

      elemDefault = {
        id: 0,
        colorName: 'AAAAAAA',
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

      it('should create a Color', () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.create(new Color()).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should update a Color', () => {
        const returnedFromService = Object.assign(
          {
            id: 1,
            colorName: 'BBBBBB',
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.update(expected).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should partial update a Color', () => {
        const patchObject = Object.assign({}, new Color());

        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = Object.assign({}, returnedFromService);

        service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PATCH' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should return a list of Color', () => {
        const returnedFromService = Object.assign(
          {
            id: 1,
            colorName: 'BBBBBB',
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

      it('should delete a Color', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });

      describe('addColorToCollectionIfMissing', () => {
        it('should add a Color to an empty array', () => {
          const color: IColor = { id: 123 };
          expectedResult = service.addColorToCollectionIfMissing([], color);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(color);
        });

        it('should not add a Color to an array that contains it', () => {
          const color: IColor = { id: 123 };
          const colorCollection: IColor[] = [
            {
              ...color,
            },
            { id: 456 },
          ];
          expectedResult = service.addColorToCollectionIfMissing(colorCollection, color);
          expect(expectedResult).toHaveLength(2);
        });

        it("should add a Color to an array that doesn't contain it", () => {
          const color: IColor = { id: 123 };
          const colorCollection: IColor[] = [{ id: 456 }];
          expectedResult = service.addColorToCollectionIfMissing(colorCollection, color);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(color);
        });

        it('should add only unique Color to an array', () => {
          const colorArray: IColor[] = [{ id: 123 }, { id: 456 }, { id: 24780 }];
          const colorCollection: IColor[] = [{ id: 123 }];
          expectedResult = service.addColorToCollectionIfMissing(colorCollection, ...colorArray);
          expect(expectedResult).toHaveLength(3);
        });

        it('should accept varargs', () => {
          const color: IColor = { id: 123 };
          const color2: IColor = { id: 456 };
          expectedResult = service.addColorToCollectionIfMissing([], color, color2);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(color);
          expect(expectedResult).toContain(color2);
        });

        it('should accept null and undefined values', () => {
          const color: IColor = { id: 123 };
          expectedResult = service.addColorToCollectionIfMissing([], null, color, undefined);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(color);
        });
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
