import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IColor } from '../color.model';

@Component({
  selector: 'jhi-color-detail',
  templateUrl: './color-detail.component.html',
})
export class ColorDetailComponent implements OnInit {
  color: IColor | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ color }) => {
      this.color = color;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
