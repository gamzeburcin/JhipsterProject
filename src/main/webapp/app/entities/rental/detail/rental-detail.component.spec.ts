import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { RentalDetailComponent } from './rental-detail.component';

describe('Component Tests', () => {
  describe('Rental Management Detail Component', () => {
    let comp: RentalDetailComponent;
    let fixture: ComponentFixture<RentalDetailComponent>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        declarations: [RentalDetailComponent],
        providers: [
          {
            provide: ActivatedRoute,
            useValue: { data: of({ rental: { id: 123 } }) },
          },
        ],
      })
        .overrideTemplate(RentalDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(RentalDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load rental on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.rental).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
