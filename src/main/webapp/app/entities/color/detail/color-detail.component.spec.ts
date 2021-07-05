import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ColorDetailComponent } from './color-detail.component';

describe('Component Tests', () => {
  describe('Color Management Detail Component', () => {
    let comp: ColorDetailComponent;
    let fixture: ComponentFixture<ColorDetailComponent>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        declarations: [ColorDetailComponent],
        providers: [
          {
            provide: ActivatedRoute,
            useValue: { data: of({ color: { id: 123 } }) },
          },
        ],
      })
        .overrideTemplate(ColorDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(ColorDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load color on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.color).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
