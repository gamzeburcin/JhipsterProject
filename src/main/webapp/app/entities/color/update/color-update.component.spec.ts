jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { ColorService } from '../service/color.service';
import { IColor, Color } from '../color.model';

import { ColorUpdateComponent } from './color-update.component';

describe('Component Tests', () => {
  describe('Color Management Update Component', () => {
    let comp: ColorUpdateComponent;
    let fixture: ComponentFixture<ColorUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let colorService: ColorService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [ColorUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(ColorUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(ColorUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      colorService = TestBed.inject(ColorService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should update editForm', () => {
        const color: IColor = { id: 456 };

        activatedRoute.data = of({ color });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(color));
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const color = { id: 123 };
        spyOn(colorService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ color });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: color }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(colorService.update).toHaveBeenCalledWith(color);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const color = new Color();
        spyOn(colorService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ color });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: color }));
        saveSubject.complete();

        // THEN
        expect(colorService.create).toHaveBeenCalledWith(color);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const color = { id: 123 };
        spyOn(colorService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ color });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(colorService.update).toHaveBeenCalledWith(color);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).not.toHaveBeenCalled();
      });
    });
  });
});
