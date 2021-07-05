jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { RentalService } from '../service/rental.service';
import { IRental, Rental } from '../rental.model';

import { RentalUpdateComponent } from './rental-update.component';

describe('Component Tests', () => {
  describe('Rental Management Update Component', () => {
    let comp: RentalUpdateComponent;
    let fixture: ComponentFixture<RentalUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let rentalService: RentalService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [RentalUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(RentalUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(RentalUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      rentalService = TestBed.inject(RentalService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should update editForm', () => {
        const rental: IRental = { id: 456 };

        activatedRoute.data = of({ rental });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(rental));
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const rental = { id: 123 };
        spyOn(rentalService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ rental });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: rental }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(rentalService.update).toHaveBeenCalledWith(rental);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const rental = new Rental();
        spyOn(rentalService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ rental });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: rental }));
        saveSubject.complete();

        // THEN
        expect(rentalService.create).toHaveBeenCalledWith(rental);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const rental = { id: 123 };
        spyOn(rentalService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ rental });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(rentalService.update).toHaveBeenCalledWith(rental);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).not.toHaveBeenCalled();
      });
    });
  });
});
