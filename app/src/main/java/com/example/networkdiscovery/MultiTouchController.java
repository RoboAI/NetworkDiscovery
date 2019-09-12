package com.example.networkdiscovery;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.HashMap;

public class MultiTouchController implements View.OnTouchListener{

	public interface TouchUpdateListener{
		/** @param touch the single fingers element  **/
		void onTouchUpdate(SingleTouch touch);
		void onTouchDown(SingleTouch touch);
		void onTouchUp(SingleTouch touch);
	}

	public class SingleTouch {
		public int id;
		public float x;
		public float y;
		//public boolean bIsDown;//not currently used
		//public int type;//not currently used
	}

	private TouchUpdateListener touchUpdateListener;
	HashMap<Integer, SingleTouch> map_pointers;

	public MultiTouchController() {
	    map_pointers = new HashMap<>();
	}

	protected void InitTouch(SingleTouch touch) {
		touch.id = 0;
		touch.x = 0;
		touch.y = 0;
	}
	
	/** @param l the new listener **/
	public void setOnTouchUpdate(TouchUpdateListener l){
		touchUpdateListener = l;
	}
	
	private void notifyOnTouchUpdate(SingleTouch t){
		if(touchUpdateListener != null){
			touchUpdateListener.onTouchUpdate(t);
		}
	}

    private void notifyOnTouchDown(SingleTouch t){
        if(touchUpdateListener != null){
            touchUpdateListener.onTouchDown(t);
        }
    }

    private void notifyOnTouchUp(SingleTouch t){
        if(touchUpdateListener != null){
            touchUpdateListener.onTouchUp(t);
        }
    }

	protected SingleTouch findTouchById(int id) {
	    return map_pointers.get(id);
	}

	private void addPointer(MotionEvent event) {
		SingleTouch touch = new SingleTouch();
		int action_index = event.getActionIndex();

		Log.i("Motion", "AddNonPrimaryPointer: " + String.valueOf(event.getPointerId(action_index)));

		InitTouch(touch);
		touch.id = event.getPointerId(action_index);
		touch.x = event.getX(action_index);
		touch.y = event.getY(action_index);
		map_pointers.put(touch.id, touch);

		notifyOnTouchDown(touch);
	}

	private void removePointer(MotionEvent event) {
		int remove_id = event.getPointerId(event.getActionIndex());

		Log.i("Motion", "removePointer: " + String.valueOf(remove_id));

		SingleTouch touch_removed = map_pointers.remove(remove_id);
		notifyOnTouchUp(touch_removed);
	}

	private void pointerUpdate(MotionEvent event) {
        Log.i("Motion",  "PointerUpdate: " + event.getPointerId(event.getActionIndex()));

        SingleTouch touch;
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; ++i) {
            touch = findTouchById(event.getPointerId(i));
            touch.x = event.getX(i);
            touch.y = event.getY(i);
            notifyOnTouchUpdate(touch);
        }
    }

	private void actionCancelled(MotionEvent event) {
		Log.i("Motion", "ActionCancelled: " + event.getPointerId(event.getActionIndex()));

        map_pointers.clear();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                addPointer(event);
                break;
            case MotionEvent.ACTION_UP:
                removePointer(event);
                break;
            case MotionEvent.ACTION_MOVE:
                pointerUpdate(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                actionCancelled(event);
                break;
            case MotionEvent.ACTION_HOVER_ENTER:
                addPointer(event);
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                removePointer(event);
                break;
            case MotionEvent.ACTION_HOVER_MOVE:
                pointerUpdate(event);
                break;
            case MotionEvent.ACTION_OUTSIDE:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                addPointer(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                removePointer(event);
                break;
            case MotionEvent.ACTION_SCROLL:
                break;
            default:
                break;
        }
		return true;
	}

}
