import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

@Injectable({
    providedIn:'root'
})
export class LoadingService{

    public loaded$: BehaviorSubject<{context:string,loaded: boolean}> = new BehaviorSubject({
        context: '',
        loaded: false   
    } as { context: string, loaded: boolean });

    public change(context: string,loaded: boolean){
        this.loaded$.next({
            context,loaded
        });
    }

}