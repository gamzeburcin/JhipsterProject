jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { CarImageService } from '../service/car-image.service';
import { ICarImage, CarImage } from '../car-image.model';

import { CarImageUpdateComponent } from './car-image-update.component';

describe('Component Tests', () => {
  describe('CarImage Management Update Component', () => {
    let comp: CarImageUpdateComponent;
    let fixture: ComponentFixture<CarImageUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let carImageService: CarImageService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [CarImageUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(CarImageUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CarImageUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      carImageService = TestBed.inject(CarImageService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should update editForm', () => {
        const carImage: ICarImage = { id: 456 };

        activatedRoute.data = of({ carImage });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(carImage));
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const carImage = { id: 123 };
        spyOn(carImageService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ carImage });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: carImage }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(carImageService.update).toHaveBeenCalledWith(carImage);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const carImage = new CarImage();
        spyOn(carImageService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ carImage });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: carImage }));
        saveSubject.complete();

        // THEN
        expect(carImageService.create).toHaveBeenCalledWith(carImage);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const carImage = { id: 123 };
        spyOn(carImageService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ carImage });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(carImageService.update).toHaveBeenCalledWith(carImage);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).not.toHaveBeenCalled();
      });
    });
  });
});
