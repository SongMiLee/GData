package data.hci.gdatawatch.Data;

/**
 * Created by 정민 on 2016-03-31.
 */

import android.util.Log;

public class FFT {

    private static final Complex ZERO = new Complex(0, 0);

    // Do not instantiate.
    private FFT() { }

    /**
     * Returns the FFT of the specified complex array.
     *
     * @param  x the complex array
     * @return the FFT of the complex array <tt>x</tt>
     * @throws IllegalArgumentException if the length of <t>x</tt> is not a power of 2
     */
    public static Complex[] fft(Complex[] x) {
        int N = x.length;

        // base case
        if (N == 1) return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) {
            throw new IllegalArgumentException("N is not a power of 2");
        }

        // fft of even terms
        Complex[] even = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < N/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + N/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }


    /**
     * Returns the inverse FFT of the specified complex array.
     *
     * @param  x the complex array
     * @return the inverse FFT of the complex array <tt>x</tt>
     * @throws IllegalArgumentException if the length of <t>x</tt> is not a power of 2
     */
    public static Complex[] ifft(Complex[] x) {
        int N = x.length;
        Complex[] y = new Complex[N];

        // take conjugate
        for (int i = 0; i < N; i++) {
            y[i] = x[i].conjugate();
        }

        // compute forward FFT
        y = fft(y);

        // take conjugate again
        for (int i = 0; i < N; i++) {
            y[i] = y[i].conjugate();
        }

        // divide by N
        for (int i = 0; i < N; i++) {
            y[i] = y[i].scale(1.0 / N);
        }

        return y;

    }

    /**
     * Returns the circular convolution of the two specified complex arrays.
     *
     * @param  x one complex array
     * @param  y the other complex array
     * @return the circular convolution of <tt>x</tt> and <tt>y</tt>
     * @throws IllegalArgumentException if the length of <t>x</tt> does not equal
     *         the length of <tt>y</tt> or if the length is not a power of 2
     */
    public static Complex[] cconvolve(Complex[] x, Complex[] y) {

        // should probably pad x and y with 0s so that they have same length
        // and are powers of 2
        if (x.length != y.length) {
            throw new IllegalArgumentException("Dimensions don't agree");
        }

        int N = x.length;

        // compute FFT of each sequence
        Complex[] a = fft(x);
        Complex[] b = fft(y);

        // point-wise multiply
        Complex[] c = new Complex[N];
        for (int i = 0; i < N; i++) {
            c[i] = a[i].times(b[i]);
        }

        // compute inverse FFT
        return ifft(c);
    }

    /**
     * Returns the linear convolution of the two specified complex arrays.
     *
     * @param  x one complex array
     * @param  y the other complex array
     * @return the linear convolution of <tt>x</tt> and <tt>y</tt>
     * @throws IllegalArgumentException if the length of <t>x</tt> does not equal
     *         the length of <tt>y</tt> or if the length is not a power of 2
     */
    public static Complex[] convolve(Complex[] x, Complex[] y) {
        Complex[] a = new Complex[2*x.length];
        for (int i = 0; i < x.length; i++)
            a[i] = x[i];
        for (int i = x.length; i < 2*x.length; i++)
            a[i] = ZERO;

        Complex[] b = new Complex[2*y.length];
        for (int i = 0; i < y.length; i++)
            b[i] = y[i];
        for (int i = y.length; i < 2*y.length; i++)
            b[i] = ZERO;

        return cconvolve(a, b);
    }

    // display an array of Complex numbers to standard output
    private static void show(Complex[] x, String title) {
        Log.d(title,"");
        Log.d("-------------------","");
        for (int i = 0; i < x.length; i++) {
            Log.d(x[i]+"","");
        }
        Log.d("","");
    }


    /***************************************************************************
     *  Test client.
     ***************************************************************************/

    /**
     * Unit tests the <tt>FFT</tt> class.
     */
    public static void main(String[] args) {
        int N = 8;//Integer.parseInt(args[0]);
        Complex[] x = new Complex[N];

        // original data
        for (int i = 0; i < N; i++) {
            x[i] = new Complex(i, 0);
            x[i] = new Complex(-2*Math.random() + 1, 0);
        }
        show(x, "x");

        // FFT of original data
        Complex[] y = fft(x);
        show(y, "y = fft(x)");

        // take inverse FFT
        Complex[] z = ifft(y);
        show(z, "z = ifft(y)");

        // circular convolution of x with itself
        Complex[] c = cconvolve(x, x);
        show(c, "c = cconvolve(x, x)");

        // linear convolution of x with itself
        Complex[] d = convolve(x, x);
        show(d, "d = convolve(x, x)");
    }

    public static class Complex {
        private final double re;   // the real part
        private final double im;   // the imaginary part

        /**
         * Initializes a complex number from the specified real and imaginary parts.
         *
         * @param real the real part
         * @param imag the imaginary part
         */
        public Complex(double real, double imag) {
            re = real;
            im = imag;
        }

        /**
         * Returns a string representation of this complex number.
         *
         * @return a string representation of this complex number,
         *         of the form 34 - 56i.
         */
        public String toString() {
            if (im == 0) return re + "";
            if (re == 0) return im + "i";
            if (im <  0) return re + " - " + (-im) + "i";
            return re + " + " + im + "i";
        }

        // return abs/modulus/magnitude and angle/phase/argument
        /**
         * Returns the absolute value of this complex number.
         * This quantity is also known as the <em>modulus</em> or <em>magnitude</em>.
         *
         * @return the absolute value of this complex number
         */
        public double abs() {
            return Math.hypot(re, im);
        }

        /**
         * Returns the phase of this complex number.
         * This quantity is also known as the <em>ange</em> or <em>argument</em>.
         *
         * @return the phase of this complex number, a real number between -pi and pi
         */
        public double phase() {
            return Math.atan2(im, re);
        }

        /**
         * Returns the sum of this complex number and the specified complex number.
         *
         * @param  that the other complex number
         * @return the complex number whose value is <tt>(this + that)</tt>
         */
        public Complex plus(Complex that) {
            double real = this.re + that.re;
            double imag = this.im + that.im;
            return new Complex(real, imag);
        }

        /**
         * Returns the result of subtracting the specified complex number from
         * this complex number.
         *
         * @param  that the other complex number
         * @return the complex number whose value is <tt>(this - that)</tt>
         */
        public Complex minus(Complex that) {
            double real = this.re - that.re;
            double imag = this.im - that.im;
            return new Complex(real, imag);
        }

        /**
         * Returns the product of this complex number and the specified complex number.
         *
         * @param  that the other complex number
         * @return the complex number whose value is <tt>(this * that)</tt>
         */
        public Complex times(Complex that) {
            double real = this.re * that.re - this.im * that.im;
            double imag = this.re * that.im + this.im * that.re;
            return new Complex(real, imag);
        }

        /**
         * Returns the product of this complex number and the specified scalar.
         *
         * @param  alpha the scalar
         * @return the complex number whose value is <tt>(alpha * this)</tt>
         */
        public Complex scale(double alpha) {
            return new Complex(alpha * re, alpha * im);
        }

        /**
         * Returns the product of this complex number and the specified scalar.
         *
         * @param  alpha the scalar
         * @return the complex number whose value is <tt>(alpha * this)</tt>
         * @deprecated Use {@link #scale(double)} instead.
         */
        public Complex times(double alpha) {
            return new Complex(alpha * re, alpha * im);
        }

        /**
         * Returns the complex conjugate of this complex number.
         *
         * @return the complex conjugate of this complex number
         */
        public Complex conjugate() {
            return new Complex(re, -im);
        }

        /**
         * Returns the reciprocal of this complex number.
         *
         * @return the complex number whose value is <tt>(1 / this)</tt>
         */
        public Complex reciprocal() {
            double scale = re*re + im*im;
            return new Complex(re / scale, -im / scale);
        }

        /**
         * Returns the real part of this complex number.
         *
         * @return the real part of this complex number
         */
        public double re() {
            return re;
        }

        /**
         * Returns the imaginary part of this complex number.
         *
         * @return the imaginary part of this complex number
         */
        public double im() {
            return im;
        }

        /**
         * Returns the result of dividing the specified complex number into
         * this complex number.
         *
         * @param  that the other complex number
         * @return the complex number whose value is <tt>(this / that)</tt>
         */
        public Complex divides(Complex that) {
            return this.times(that.reciprocal());
        }

        /**
         * Returns the complex exponential of this complex number.
         *
         * @return the complex exponential of this complex number
         */
        public Complex exp() {
            return new Complex(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
        }

        /**
         * Returns the complex sine of this complex number.
         *
         * @return the complex sine of this complex number
         */
        public Complex sin() {
            return new Complex(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
        }

        /**
         * Returns the complex cosine of this complex number.
         *
         * @return the complex cosine of this complex number
         */
        public Complex cos() {
            return new Complex(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
        }

        /**
         * Returns the complex tangent of this complex number.
         *
         * @return the complex tangent of this complex number
         */
        public Complex tan() {
            return sin().divides(cos());
        }


        /**
         * Unit tests the <tt>Complex</tt> data type.
         */
    }

}
