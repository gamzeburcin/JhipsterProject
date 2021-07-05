import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { IColor, Color } from '../color.model';
import { ColorService } from '../service/color.service';

@Component({
  selector: 'jhi-color-update',
  templateUrl: './color-update.component.html',
})
export class ColorUpdateComponent implements OnInit {
  isSaving = false;

  editForm = this.fb.group({
    id: [],
    colorName: [],
  });

  constructor(protected colorService: ColorService, protected activatedRoute: ActivatedRoute, protected fb: FormBuilder) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ color }) => {
      this.updateForm(color);
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const color = this.createFromForm();
    if (color.id !== undefined) {
      this.subscribeToSaveResponse(this.colorService.update(color));
    } else {
      this.subscribeToSaveResponse(this.colorService.create(color));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IColor>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(color: IColor): void {
    this.editForm.patchValue({
      id: color.id,
      colorName: color.colorName,
    });
  }

  protected createFromForm(): IColor {
    return {
      ...new Color(),
      id: this.editForm.get(['id'])!.value,
      colorName: this.editForm.get(['colorName'])!.value,
    };
  }
}
