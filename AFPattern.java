abstract class Eagle
{
abstract void hunt();
static Eagle getEagle(String st)
{
if(st.equals("G"))
{
 return new Golden();
}
else if(st.equals("S"))
{
 return new Serpent();
}
else 
{
 return new Mountain();
}
}
}

class Golden extends Eagle
{
void hunt()
{
System.out.println("type of eagle is golden");
}
}

class Serpent extends Eagle
{
void hunt()
{
System.out.println("type of eagle is serpent");
}
}

class Mountain extends Eagle
{
void hunt()
{
System.out.println("type of eagle is mountain");
}
}

public class AFPattern
{
public static void main(String args[])
{
Eagle e= Eagle.getEagle(args[0]);
e.hunt();
}
}

