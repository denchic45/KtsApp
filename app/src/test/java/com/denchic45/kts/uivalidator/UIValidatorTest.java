package com.denchic45.kts.uivalidator;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;



//@RunWith(JUnit4.class)
public class UIValidatorTest {
//
//    public static final String SUCCESS_MESSAGE = "Все верно!";
//    public static final String ERROR_MESSAGE = "Ошибка!";
//    @org.junit.Rule
//    public InstantTaskExecutorRule mInstantTaskExecutorRule = new InstantTaskExecutorRule();
//    private UIValidator uiValidator;
//
//    @Before
//    public void setUp() {
//        uiValidator = new UIValidator();
//    }
//
//    @After
//    public void tearDown() {
//
//    }
//
//    @Test
//    public void testErrorSimpleValidation() {
//        MutableLiveData<String> showErrorMessage = new MutableLiveData<>();
//
//        uiValidator.addValidation(new Validation(new Rule(() -> true, "")).onErrorSendMessage(showErrorMessage::setValue));
//
////        uiValidator.addValidation(new SimpleValidation(showErrorMessage, SUCCESS_MESSAGE, new Rule(() -> false, ERROR_MESSAGE)));
////        showErrorMessage.observeForever(s -> assertEquals(ERROR_MESSAGE, s));
//
//        uiValidator.runValidates();
//
//
//    }
//
//    @Test
//    public void testSuccessAfterErrorValidation() {
//        MutableLiveData<String> showMessage = new MutableLiveData<>();
//        AtomicBoolean condition = new AtomicBoolean(false);
//        uiValidator.addValidation(new Validation(new Rule(condition::get, ERROR_MESSAGE)).onErrorSendMessage(showMessage::setValue).onSuccessRun(() -> showMessage.setValue(SUCCESS_MESSAGE)));
//
//        Observer<String> successObserver = new Observer<String>() {
//            @Override
//            public void onChanged(String s) {
//                assertEquals(SUCCESS_MESSAGE, s);
//                showMessage.removeObserver(this);
//            }
//        };
//
//        Observer<String> errorObserver = new Observer<String>() {
//            @Override
//            public void onChanged(String s) {
//                assertEquals(ERROR_MESSAGE, s);
//                showMessage.removeObserver(this);
//                condition.set(true);
//                uiValidator.runValidates();
//                showMessage.observeForever(successObserver);
//            }
//        };
//        showMessage.observeForever(errorObserver);
//
//        uiValidator.runValidates();
//    }
//
//    @Test
//    public void testMultipleConditionsValidation() {
//        MutableLiveData<String> showSuccessMessage = new MutableLiveData<>();
//        MutableLiveData<String> showErrorMessage = new MutableLiveData<>();
//
//        uiValidator.addValidation(new Validation(new Rule(() -> true, ERROR_MESSAGE), new Rule(() -> true, ERROR_MESSAGE)).onErrorSendMessage(showErrorMessage::setValue).onSuccessRun(() -> showSuccessMessage.setValue(null)));
//        uiValidator.addValidation(new Validation(new Rule(() -> false, ERROR_MESSAGE)).onErrorSendMessage(showErrorMessage::setValue));
//
//        uiValidator.runValidates();
//
//        showSuccessMessage.observeForever(Assert::assertNull);
//
//        showErrorMessage.observeForever(s -> assertEquals(ERROR_MESSAGE, s));
//    }
//
//    @Test
//    public void testFieldValidation() {
//        MutableLiveData<Map.Entry<Integer, String>> showSuccessMessage = new MutableLiveData<>();
//        MutableLiveData<Map.Entry<Integer, String>> showErrorMessage = new MutableLiveData<>();
//
////        uiValidator.addValidation(new FieldValidation(showSuccessMessage, 1, new Rule(() -> true,"")));
////
////        uiValidator.addValidation(new FieldValidation(showErrorMessage, 2));
//
//        uiValidator.runValidates();
//
//        showSuccessMessage.observeForever(integerStringPair -> {
//            System.out.println(integerStringPair.getKey());
//            System.out.println(integerStringPair.getValue());
//            assertEquals(SUCCESS_MESSAGE, integerStringPair.getValue());
//        });
//
//        showErrorMessage.observeForever(integerStringPair -> {
//            System.out.println(integerStringPair.getKey());
//            System.out.println(integerStringPair.getValue());
//            assertEquals(ERROR_MESSAGE, integerStringPair.getValue());
//        });
//    }
//
//    @Test
//    public void testSomething() {
//        MutableLiveData<Integer> resIdLiveData = new MutableLiveData<>();
//        Validation simpleValidation = new Validation(new Rule(() -> true, 123));
//        simpleValidation.onErrorSendIdRes(resIdLiveData::setValue);
//        uiValidator.addValidation(simpleValidation);
//    }
}
