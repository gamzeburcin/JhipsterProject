import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ICarImage } from '../car-image.model';

@Component({
  selector: 'jhi-car-image-detail',
  templateUrl: './car-image-detail.component.html',
})
export class CarImageDetailComponent implements OnInit {
  carImage: ICarImage | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ carImage }) => {
      this.carImage = carImage;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
