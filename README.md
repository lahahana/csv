# csv
Annotation based CSV serialize tool

### # 这是一个根据Java Class Field进行CSV序列化的工具，基于注解的形式，只需要在相关类上添加注解，就可以实现自定义的CSV序列化方案

通过调用`CsvSerializer.serialize(*)`来进行CSV序列化

相关注解：
* `@CsvIgnore` :此注解为标识性注解，`@Target({ElementType.FIELD})`, 表示该字段在序列化时会被忽略
* `@CsvIgnoreProperties`：此注解`@Target({ElementType.TYPE})`，在类级别指定当前类所拥有字段的哪些在序列化时忽略
* `@CsvProperty`：此注解`@Target({ElementType.TYPE})`，通过指定此注解的`header`，`order`,分别可以实现在序列化时指定指定的字段的csv header的值，字段的序列化顺序。最终要的一个属性是`converter`，如果需要对特定字段在序列化时进行一些数据转换的操作，可以通过继承`com.alpha.csv.convertor.Converter`接口,实现自定义的Converter。

**Demo：**
    
    @CsvIgnoreProperties({"country"})    
    public class Address {
    
        private String country = "CN";
    
        @CsvIgnore
        public String state = "ZJ";
    
        private String city = "Hangzhou";
    
    }

    public class Person {
    
        @CsvProperty(header="Name", order=1)
        private String name = "Lahahana";

        @CsvProperty(header="ID", order=0)
        private int id = 10001;
    
        @CsvProperty(header = "Birthday", order=2, converter = TimeToDateConvertor.class)
        public long birthday= System.currentTimeMillis();
    
        private Address address = new Address();
    
        public static void main(String []args) throws CsvException, IOException {
            List list = new ArrayList();
            Person p = new Person();
            for(int i=0;i<10;i++)
                list.add(p);    
            String info = CsvSerializer.serialize(list, Person.class);
            System.out.println(info);
        }
    }


**Result:**  
    ID,Name,Birthday,city  
    10001,Lahahana,Tue Mar 28 21:14:59 GMT+08:00 2017,Hangzhou  
    10001,Lahahana,Tue Mar 28 21:14:59 GMT+08:00 2017,Hangzhou  

