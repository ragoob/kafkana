import { ThrowStmt } from '@angular/compiler';
import { Component, OnInit, forwardRef, Input, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { fromEvent, ReplaySubject } from 'rxjs';
import { debounceTime, filter, takeUntil } from 'rxjs/operators';
interface values {
    key: string,
    value: string
}
@Component({
  selector: 'app-map-control',
  templateUrl: './map-control.component.html',
  styleUrls: ['./map-control.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MapControlComponent),
      multi: true
    }
  ]
})
export class MapControlComponent implements OnInit, ControlValueAccessor{
 
  public values: values[] = [{key: "", value: ""}];
  public value: any = {};
  onChange: any = () => { };
  onTouched: any = () => { };
  constructor() { }

  writeValue(obj: any): void {
   
  }
  registerOnChange(fn: any): void {
    this.onChange = fn;
  }
  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  ngOnInit(): void {
  }

  
  public delete(index: number){
    this.values.splice(index,1);
    this.updateMap();
  }

  public newRow(item: values){
    if(!item || !item.key || !item.value)
    return;
    const index = this.values.findIndex(res=> res.key == item.key);
    if(this.values.length - 1 === index){
      this.values.push({
        key: "",
        value: ""
      });
    }

    this.updateMap();
  }

  public updateMap(): void{
   this.value = {};
    this.values.forEach(res => {
      this.value[res.key] = res.value.trim()
    });

    this.onChange(this.value);
  
  }

  
}
