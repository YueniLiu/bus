
package plannist;

import java.util.HashMap;
import java.util.Map;

public class Session{
    Map session;

    public Session(){
       session = new HashMap<>();
    }

    public void add(String key, Object val){
        session.put(key, val);
    }

    public Object get(String key){
        if(session.containsKey(key)) {
            return session.get(key);
        }
        else {
            return null;
        }
    }

    public void remove(String key){
        if(session.containsKey(key)) {
            session.remove(key);
        }
    }

    public String[] getKeyList(){
        Object[] keys = session.keySet().toArray();
        int N = keys.length;
        String[] keyStrs = new String[N];
        for(int i = 0; i < N; i ++){
            keyStrs[i] = (String)keys[i];
        }
        return keyStrs;
    }

    public void removeAll(){
        session.clear();
    }

    public Map getSession(){
        return session;
    }
}
