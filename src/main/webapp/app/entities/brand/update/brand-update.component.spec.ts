jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { BrandService } from '../service/brand.service';
import { IBrand, Brand } from '../brand.model';

import { BrandUpdateComponent } from './brand-update.component';

describe('Component Tests', () => {
  describe('Brand Management Update Component', () => {
    let comp: BrandUpdateComponent;
    let fixture: ComponentFixture<BrandUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let brandService: BrandService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [BrandUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(BrandUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(BrandUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      brandService = TestBed.inject(BrandService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should update editForm', () => {
        const brand: IBrand = { id: 456 };

        activatedRoute.data = of({ brand });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(brand));
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const brand = { id: 123 };
        spyOn(brandService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ brand });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: brand }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(brandService.update).toHaveBeenCalledWith(brand);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const brand = new Brand();
        spyOn(brandService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ brand });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: brand }));
        saveSubject.complete();

        // THEN
        expect(brandService.create).toHaveBeenCalledWith(brand);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const brand = { id: 123 };
        spyOn(brandService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ brand });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(brandService.update).toHaveBeenCalledWith(brand);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).not.toHaveBeenCalled();
      });
    });
  });
});
