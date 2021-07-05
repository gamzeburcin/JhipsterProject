import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import * as dayjs from 'dayjs';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ICarImage, CarImage } from '../car-image.model';

import { CarImageService } from './car-image.service';

describe('Service Tests', () => {
  describe('CarImage Service', () => {
    let service: CarImageService;
    let httpMock: HttpTestingController;
    let elemDefault: ICarImage;
    let expectedResult: ICarImage | ICarImage[] | boolean | null;
    let currentDate: dayjs.Dayjs;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
      });
      expectedResult = null;
      service = TestBed.inject(CarImageService);
      httpMock = TestBed.inject(HttpTestingController);
      currentDate = dayjs();

      elemDefault = {
        id: 0,
        carId: 0,
        imagePath: 'AAAAAAA',
        date: currentDate,
      };
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign(
          {
            date: currentDate.format(DATE_TIME_FORMAT),
          },
          elemDefault
        );

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(elemDefault);
      });

      it('should create a CarImage', () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            date: currentDate.format(DATE_TIME_FORMAT),
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            date: currentDate,
          },
          returnedFromService
        );

        service.create(new CarImage()).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should update a CarImage', () => {
        const returnedFromService = Object.assign(
          {
            id: 1,
            carId: 1,
            imagePath: 'BBBBBB',
            date: currentDate.format(DATE_TIME_FORMAT),
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            date: currentDate,
          },
          returnedFromService
        );

        service.update(expected).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should partial update a CarImage', () => {
        const patchObject = Object.assign(
          {
            carId: 1,
            date: currentDate.format(DATE_TIME_FORMAT),
          },
          new CarImage()
        );

        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = Object.assign(
          {
            date: currentDate,
          },
          returnedFromService
        );

        service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PATCH' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should return a list of CarImage', () => {
        const returnedFromService = Object.assign(
          {
            id: 1,
            carId: 1,
            imagePath: 'BBBBBB',
            date: currentDate.format(DATE_TIME_FORMAT),
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            date: currentDate,
          },
          returnedFromService
        );

        service.query().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a CarImage', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });

      describe('addCarImageToCollectionIfMissing', () => {
        it('should add a CarImage to an empty array', () => {
          const carImage: ICarImage = { id: 123 };
          expectedResult = service.addCarImageToCollectionIfMissing([], carImage);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(carImage);
        });

        it('should not add a CarImage to an array that contains it', () => {
          const carImage: ICarImage = { id: 123 };
          const carImageCollection: ICarImage[] = [
            {
              ...carImage,
            },
            { id: 456 },
          ];
          expectedResult = service.addCarImageToCollectionIfMissing(carImageCollection, carImage);
          expect(expectedResult).toHaveLength(2);
        });

        it("should add a CarImage to an array that doesn't contain it", () => {
          const carImage: ICarImage = { id: 123 };
          const carImageCollection: ICarImage[] = [{ id: 456 }];
          expectedResult = service.addCarImageToCollectionIfMissing(carImageCollection, carImage);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(carImage);
        });

        it('should add only unique CarImage to an array', () => {
          const carImageArray: ICarImage[] = [{ id: 123 }, { id: 456 }, { id: 44825 }];
          const carImageCollection: ICarImage[] = [{ id: 123 }];
          expectedResult = service.addCarImageToCollectionIfMissing(carImageCollection, ...carImageArray);
          expect(expectedResult).toHaveLength(3);
        });

        it('should accept varargs', () => {
          const carImage: ICarImage = { id: 123 };
          const carImage2: ICarImage = { id: 456 };
          expectedResult = service.addCarImageToCollectionIfMissing([], carImage, carImage2);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(carImage);
          expect(expectedResult).toContain(carImage2);
        });

        it('should accept null and undefined values', () => {
          const carImage: ICarImage = { id: 123 };
          expectedResult = service.addCarImageToCollectionIfMissing([], null, carImage, undefined);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(carImage);
        });
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
