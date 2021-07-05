import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import * as dayjs from 'dayjs';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IRental, Rental } from '../rental.model';

import { RentalService } from './rental.service';

describe('Service Tests', () => {
  describe('Rental Service', () => {
    let service: RentalService;
    let httpMock: HttpTestingController;
    let elemDefault: IRental;
    let expectedResult: IRental | IRental[] | boolean | null;
    let currentDate: dayjs.Dayjs;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
      });
      expectedResult = null;
      service = TestBed.inject(RentalService);
      httpMock = TestBed.inject(HttpTestingController);
      currentDate = dayjs();

      elemDefault = {
        id: 0,
        rentDate: currentDate,
        returnDate: currentDate,
        customerId: 0,
        carId: 0,
      };
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign(
          {
            rentDate: currentDate.format(DATE_TIME_FORMAT),
            returnDate: currentDate.format(DATE_TIME_FORMAT),
          },
          elemDefault
        );

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(elemDefault);
      });

      it('should create a Rental', () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            rentDate: currentDate.format(DATE_TIME_FORMAT),
            returnDate: currentDate.format(DATE_TIME_FORMAT),
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            rentDate: currentDate,
            returnDate: currentDate,
          },
          returnedFromService
        );

        service.create(new Rental()).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should update a Rental', () => {
        const returnedFromService = Object.assign(
          {
            id: 1,
            rentDate: currentDate.format(DATE_TIME_FORMAT),
            returnDate: currentDate.format(DATE_TIME_FORMAT),
            customerId: 1,
            carId: 1,
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            rentDate: currentDate,
            returnDate: currentDate,
          },
          returnedFromService
        );

        service.update(expected).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should partial update a Rental', () => {
        const patchObject = Object.assign(
          {
            customerId: 1,
            carId: 1,
          },
          new Rental()
        );

        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = Object.assign(
          {
            rentDate: currentDate,
            returnDate: currentDate,
          },
          returnedFromService
        );

        service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PATCH' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should return a list of Rental', () => {
        const returnedFromService = Object.assign(
          {
            id: 1,
            rentDate: currentDate.format(DATE_TIME_FORMAT),
            returnDate: currentDate.format(DATE_TIME_FORMAT),
            customerId: 1,
            carId: 1,
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            rentDate: currentDate,
            returnDate: currentDate,
          },
          returnedFromService
        );

        service.query().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a Rental', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });

      describe('addRentalToCollectionIfMissing', () => {
        it('should add a Rental to an empty array', () => {
          const rental: IRental = { id: 123 };
          expectedResult = service.addRentalToCollectionIfMissing([], rental);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(rental);
        });

        it('should not add a Rental to an array that contains it', () => {
          const rental: IRental = { id: 123 };
          const rentalCollection: IRental[] = [
            {
              ...rental,
            },
            { id: 456 },
          ];
          expectedResult = service.addRentalToCollectionIfMissing(rentalCollection, rental);
          expect(expectedResult).toHaveLength(2);
        });

        it("should add a Rental to an array that doesn't contain it", () => {
          const rental: IRental = { id: 123 };
          const rentalCollection: IRental[] = [{ id: 456 }];
          expectedResult = service.addRentalToCollectionIfMissing(rentalCollection, rental);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(rental);
        });

        it('should add only unique Rental to an array', () => {
          const rentalArray: IRental[] = [{ id: 123 }, { id: 456 }, { id: 6666 }];
          const rentalCollection: IRental[] = [{ id: 123 }];
          expectedResult = service.addRentalToCollectionIfMissing(rentalCollection, ...rentalArray);
          expect(expectedResult).toHaveLength(3);
        });

        it('should accept varargs', () => {
          const rental: IRental = { id: 123 };
          const rental2: IRental = { id: 456 };
          expectedResult = service.addRentalToCollectionIfMissing([], rental, rental2);
          expect(expectedResult).toHaveLength(2);
          expect(expectedResult).toContain(rental);
          expect(expectedResult).toContain(rental2);
        });

        it('should accept null and undefined values', () => {
          const rental: IRental = { id: 123 };
          expectedResult = service.addRentalToCollectionIfMissing([], null, rental, undefined);
          expect(expectedResult).toHaveLength(1);
          expect(expectedResult).toContain(rental);
        });
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
