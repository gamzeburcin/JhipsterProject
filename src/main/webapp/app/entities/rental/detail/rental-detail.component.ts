import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IRental } from '../rental.model';

@Component({
  selector: 'jhi-rental-detail',
  templateUrl: './rental-detail.component.html',
})
export class RentalDetailComponent implements OnInit {
  rental: IRental | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ rental }) => {
      this.rental = rental;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
