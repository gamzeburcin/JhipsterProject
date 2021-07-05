import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { CarImageDetailComponent } from './car-image-detail.component';

describe('Component Tests', () => {
  describe('CarImage Management Detail Component', () => {
    let comp: CarImageDetailComponent;
    let fixture: ComponentFixture<CarImageDetailComponent>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        declarations: [CarImageDetailComponent],
        providers: [
          {
            provide: ActivatedRoute,
            useValue: { data: of({ carImage: { id: 123 } }) },
          },
        ],
      })
        .overrideTemplate(CarImageDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(CarImageDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load carImage on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.carImage).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
