import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrokersDetailsComponent } from './brokers-details.component';

describe('BrokersDetailsComponent', () => {
  let component: BrokersDetailsComponent;
  let fixture: ComponentFixture<BrokersDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BrokersDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BrokersDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
