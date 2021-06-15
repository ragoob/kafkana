import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddNewClusterButtonComponent } from './add-new-cluster-button.component';

describe('AddNewClusterButtonComponent', () => {
  let component: AddNewClusterButtonComponent;
  let fixture: ComponentFixture<AddNewClusterButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AddNewClusterButtonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AddNewClusterButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
